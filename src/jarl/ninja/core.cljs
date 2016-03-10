(ns ^:figwheel-always jarl.ninja.core
(:require-macros [cljs.core.async.macros :refer [go]])
(:require
          [om-tools.dom :as d :include-macros true]
          [om.core :as om]
          [om.dom :as dom]
          [secretary.core :as secretary :refer-macros [defroute]]
          [goog.events :as events]
          [goog.events :as events]
          [goog.string.format]
          [goog.history.EventType :as EventType]
          [markdown.core :refer [md->html]]
          [cljs-http.client :as http]
          [cljs.core.async :as async :refer [<!]]
          [clojure.string :as string])
   (:import goog.History))
(def format goog.string.format)
(defonce history (History.))
(enable-console-print!)
(defonce app-state (atom {:current "" :path [] :document "" :class "current" :site []}))

(defn nav-item [state]
  (om/component
  (let [page (:page state)
        current-path (:current-path state)
        path (:path state)
        current (:current state)
        classname (if (nil? current-path)
                    "children"
                   (if (empty? current-path)
                    (if  (= current (:resource page)) "selected" "peer" )
                    (let [[current-segment & rest]current-path]
                        (if (= current-segment  (:resource page))
                          "parent" "unrelated")
                    )))]
          (om.dom/li #js {:className classname}
            (if (= "selected" classname)
               (dom/div {} (:name page))
               (dom/a #js {:href (format "#%s" (str (string/join "/" path) "/" (:resource page)))} (:name page))
             )
             (if (:pages page)
                (apply om.dom/ul {}
                   (om/build-all  #(nav-item {:current current :page % :path (conj path (:resource page)) :current-path
                                              (if (or (nil? current-path) (empty? current-path) (= "unrelated" classname))
                                                nil
                                                (pop current-path)
                                                )}) (:pages page))
                 )
                 nil
              )
            )


      )
    )

)

(defn nav-item-old [state]
  (om/component
  (let [page (:page state)
        current-path (:current-path state)
        path (:path state)
        current (:current state)]
        (if  (empty? current-path)
          (if (= current (:resource page))
            (om.dom/li #js {:className (if (:pages page) "selected-with-children" "selected")}
                (dom/div {} (:name page))

                  (if (:pages page)
                  (apply om.dom/ul {}
                     (om/build-all  #(nav-item {:current nil :page % :path (conj path (:resource page)) :current-path  current-path}) (:pages page))
                         )
                    nil
                    )
                 )
            (om.dom/li {}
                (dom/a #js {:href (format "#%s" (str (string/join "/" path) "/" (:resource page)))} (:name page))
             )
          )
        (let [[current-segment & rest]current-path]
          (if (= current-segment  (:resource page))
            (om.dom/li #js {:className "parent"}

                (dom/a #js {:href (format "#%s" (str (string/join "/" path) "/" (:resource page)))} (:name page))
                  (apply om.dom/ul {}
                     (om/build-all  #(nav-item {:current current :page % :path (conj path current-segment) :current-path  rest}) (:pages page))
                )
                 )
            (om.dom/li {}
                (dom/a #js {:href (format "#%s" (str (string/join "/" path) "/" (:resource page)))} (:name page))
             )
             )
          )
        )
      )
    )

)

(defn main-menu [state owner]
  (om/component
    (dom/div  #js {:className "menu"}
        (om.dom/input #js {:type "checkbox" :className "nav-menu"  :id "nav-menu"})
       (om.dom/nav {}
         (apply om.dom/ul {}
           (om/build-all  #(nav-item {:current (:current state) :page % :path [] :current-path (:path state)}) (:pages (:site state)))
          )
        (om.dom/label #js {:htmlFor  "nav-menu" :className "nav-handle" } "Pages")
      )
    )
  )
)

(defn  content [document owner]
  (reify
    om/IRender
    (render [this]
      (om.dom/div #js {:dangerouslySetInnerHTML #js {:__html document}} nil)))
  )

(defn content-wrapper  [state owner]

  (reify
    om/IRender
    (render [this]
    (om.dom/div #js {:className (:class state)}
    (om/build content (:document state) {})
    )
    )
  ))
(defn main [state owner]
    (om/component
     (dom/div {}
        (om/build content-wrapper state {})
        (om/build main-menu  state {})
      ))
)

(om/root main app-state
           {:target (. js/document (getElementById "app"))})

(defn indexof_page[allpages resource]
  (:index(first (filter #(= resource (:resource %)) allpages)))
  )

(defn get-page[pages page path];TODO errors?
  (let [[element & rest] path]
    (if element
      (get-page (:pages (first (filter #(= element (:resource %)) pages))) page rest)
      (first (filter #(= page (:resource %)) pages))
    )
  )
)

(defn get-direction [state page path]
  (let [allpages (:pages (:site state))]
      (if (> (indexof_page allpages page) (indexof_page allpages (:current  state))) :up :down )
    )
  )
(defn load-page [page path];;Jesus this is ugly
  (let [state (deref app-state)]
    (let [allpages (:pages (:site state))]
      (let [direction (get-direction state page path)]
      (println  (str "Load page " (string/join "/" path ) "/" page))
        (case direction
          :right  (swap! app-state assoc :class "out-left")
          :left (swap! app-state assoc :class "out-right")
          :up  (swap! app-state assoc :class "out-down")
          :down (swap! app-state assoc :class "out-up"))
      (swap! app-state assoc :current page)
      (swap! app-state assoc :path path)
      (js/setTimeout (fn []
        (swap! app-state assoc :document "")
        (case direction
          :right  (swap! app-state assoc :class "new-right")
          :left (swap! app-state assoc :class "new-left")
          :up  (swap! app-state assoc :class "new-up")
          :down (swap! app-state assoc :class "new-down"))

        (js/setTimeout (fn []
        (go
              (println (str "Loading site/markdown/" (:markdown(first (filter #(= page (:resource %)) allpages)))))
              (let [response (<! (http/get (str "site/markdown/" (:markdown(get-page allpages page path)))))]
                (println  "Loaded")
                (swap! app-state assoc :document (md->html(:body response)))
                (swap! app-state assoc :class "current")
              )
         ))
        200))
      1000)
      )

    )
  )
  )



(defn add-index [hash]
  (if (:pages hash)
    (assoc hash :pages (into (vector)( map
                           #(into (hash-map)
                                  (apply vector  [:index %1] %2))
                           (iterate inc (int 0))
                           (map add-index (:pages hash)))))
    hash
    )
)
(defn addpage [page prefix]

  (println  (str "Creating route " prefix (:resource page)))
  (defroute (str prefix (:resource page))[]
    (load-page (:resource page) (into (vector) (filter #(not (string/blank? %)) (string/split prefix #"/")))))
  )

(defn load-routes [pages prefix]
  (doseq [page (:pages pages)]
    (addpage page prefix)
    (if (:pages page) (load-routes  page (str prefix (:resource page) "/")) nil)
  )
)


(go
    (let [response (<! (http/get "site/site.json"))]
        (swap! app-state assoc :site (add-index (:body response)))
        (load-routes (:body response) "/")
        (println "Routes loaded. Dispatching..")

         (goog.events/listen history EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
         (doto history (.setEnabled true))

      )
 )

