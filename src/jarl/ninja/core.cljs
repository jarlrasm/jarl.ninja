(ns ^:figwheel-always jarl.ninja.core
(:require-macros [cljs.core.async.macros :refer [go]])
(:require
          [jarl.ninja.main-menu :as main-menu]
          [om-tools.dom :as d :include-macros true]
          [om.core :as om]
          [om.dom :as dom]
          [secretary.core :as secretary :refer-macros [defroute]]
          [goog.events :as events]
          [goog.string.format]
          [goog.history.EventType]
          [goog.events.KeyHandler.EventType]
          [markdown.core :refer [md->html]]
          [cljs-http.client :as http]
          [cljs.core.async :as async :refer [put! chan <!]]
          [clojure.string :as string])
   (:import goog.History))
(defonce history (History.))
(enable-console-print!)
(defonce app-state (atom {:current "" :path [] :document "" :class "current" :site []}));Not happy about the :class


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
        (om/build main-menu/menu  state {})
      ))
)

(om/root main app-state
           {:target (. js/document (getElementById "app"))})

(defn indexof_page[allpages resource]
  (:index(first (filter #(= resource (:resource %)) allpages)))
  )

(defn pages-at-path [pages path]
  (let [[element & rest] path]
    (if element
      (pages-at-path  (:pages (first (filter #(= element (:resource %)) pages))) rest)
      pages
    )
  )
  )

(defn get-page[pages resource path]
 (first (filter #(= resource (:resource %)) (pages-at-path pages path)))
)

(defn get-direction [state page path]
  (let [old-path (:path state)]
    (if (= (count path) (count old-path) )
      (let [allpages (:pages (:site state))]
          (if (> (indexof_page allpages page) (indexof_page allpages (:current  state))) :down :up )
        )

      (if (> (count path) (count old-path))
        :right
        :left
        )
     )
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

(defn right []
  (let [state (deref app-state)
        path (:path state)
        current (get-page (:pages (:site state))  (:current state) path)
        ]
        (if (not (empty? (:pages current)))
          (let [next (first(:pages current))]
          (secretary/dispatch! (str (string/join "/" (:path state)) "/" (:current state) "/" (:resource next) ) )
          )
          nil)

    )
 )
(defn left[]
  (let [state (deref app-state)
        path (:path state)
        ]
      (if(not (empty? path))
        (secretary/dispatch! (string/join "/" (:path state))   )
        nil)

    )
 )
(defn up [] ;; TODO Refaktor
  (let [state (deref app-state)]
    (let [allpages (pages-at-path (:pages (:site state)) (:path state))
          current (get-page (:pages (:site state))  (:current state) (:path state))]
        (let [next (first(sort #(> (:index %1) (:index %2))(filter #(< (:index %)(:index current))allpages)))]
          (if next
            (secretary/dispatch! (str (string/join "/" (:path state)) "/" (:resource next) ) )
            nil

            )
          )
        )
      )
  )
(defn down [] ;; TODO Refaktor
  (let [state (deref app-state)]
    (let [allpages (pages-at-path (:pages (:site state)) (:path state))
          current (get-page (:pages (:site state))  (:current state) (:path state))]
        (let [next (first(sort #(< (:index %1) (:index %2))(filter #(> (:index %)(:index current))allpages)))]
          (if next
            (secretary/dispatch! (str (string/join "/" (:path state)) "/" (:resource next) ) )
            nil

            )
          )
        )
      )
  )

(defn key-pressed [key]
  (case key
    37 (left)
    39 (right)
    38 (up)
    40 (down)
    nil
  )
)
(go
    (let [response (<! (http/get "site/site.json"))]
        (swap! app-state assoc :site (add-index (:body response)))
        (load-routes (:body response) "/")
        (println "Routes loaded. Dispatching..")

         (goog.events/listen history goog.history.EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
         (goog.events/listen  js/document "keydown" #( key-pressed(.-keyCode (.-event_ %))))
         (doto history (.setEnabled true))

      )
 )

