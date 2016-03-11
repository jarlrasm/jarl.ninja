(ns ^:figwheel-always jarl.ninja.routing
  (:require
          [secretary.core :as secretary :refer-macros [defroute]]
          [jarl.ninja.content :as content]
          [clojure.string :as string]
))


(defn with-page-index [hash]
  (if (:pages hash)
    (assoc hash :pages (into (vector)( map
                           #(into (hash-map)
                                  (apply vector  [:index %1] %2))
                           (iterate inc (int 0))
                           (map with-page-index (:pages hash)))))
    hash
    )
)
(defn add-route [app-state page prefix]

  (println  (str "Creating route " prefix (:resource page)))
  (defroute (str prefix (:resource page))[]
    (content/load-page! app-state (:resource page) (into (vector) (filter #(not (string/blank? %)) (string/split prefix #"/")))))
  )

(defn load-routes [app-state pages prefix]
  (doseq [page (:pages pages)]
    ( add-route app-state  page prefix)
    (when (:pages page) (load-routes app-state  page (str prefix (:resource page) "/")))
  )
)

(defn load-site! [app-state site]

  (swap! app-state assoc :site (with-page-index site))
  (load-routes app-state site "/")
  )
