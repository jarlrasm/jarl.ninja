(ns ^:figwheel-always jarl.ninja.main-menu
(:require
          [om.core :as om]
          [jarl.ninja.routing :as routing]
          [om.dom :as dom]))

(defn li-classname [current-path current page]
   (if (nil? current-path)
                    "children"
                   (if (empty? current-path)
                    (if  (= current (:resource page)) "selected" "peer" )
                    (let [[current-segment & rest]current-path]
                        (if (= current-segment  (:resource page))
                          "parent" "unrelated")
                    )))
  )

(defn nav-item [state]
  (om/component
    (let [page (:page state)
          current-path (:current-path state)
          path (:path state)
          current (:current state)
          classname (li-classname current-path current page)]
          (om.dom/li #js {:className classname}
            (if (= "selected" classname)
               (dom/div {} (:name page))
               (dom/a #js {:href (str "#" (routing/get-route-to-resource path(:resource page)))} (:name page))
             )
             (when (:pages page)
                (apply om.dom/ul {}
                   (om/build-all  #(nav-item {:current current :page % :path (conj path (:resource page)) :current-path
                                              (if (or (nil? current-path) (empty? current-path) (= "unrelated" classname))
                                                nil
                                                (pop current-path)
                                                )}) (:pages page))
                 )
               )
            )

        )
    )

)


(defn menu [state owner]
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
