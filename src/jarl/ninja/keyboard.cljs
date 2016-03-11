(ns ^:figwheel-always jarl.ninja.keyboard
(:require [jarl.ninja.navigation :as navigation]
          [jarl.ninja.routing :as routing]
          [clojure.string :as string]))


(defn right! [state]
  (let [path (:path state)
        current (navigation/get-page (:pages (:site state))  (:current state) path)
        ]
        (if (not (empty? (:pages current)))
          (let [next (first(:pages current))]
          (routing/goto!  (str (string/join "/" (:path state)) "/" (:current state) "/" (:resource next) ) )
          )
          nil)

    )
 )
(defn left![state]
  (let [path (:path state)        ]
      (if(not (empty? path))
        (routing/goto!  (string/join "/" path)   )
        nil)

    )
 )
(defn up! [state]
    (let [allpages (navigation/pages-at-path (:pages (:site state)) (:path state))
          current (navigation/get-page (:pages (:site state))  (:current state) (:path state))]
        (let [next (first(sort #(> (:index %1) (:index %2))(filter #(< (:index %)(:index current))allpages)))]
          (if next
            (routing/goto! (str (string/join "/" (:path state)) "/" (:resource next) ) )
            nil

            )
          )
        )
      )

(defn down! [state]
    (let [allpages (navigation/pages-at-path (:pages (:site state)) (:path state))
          current (navigation/get-page (:pages (:site state))  (:current state) (:path state))]
        (let [next (first(sort #(< (:index %1) (:index %2))(filter #(> (:index %)(:index current))allpages)))]
          (if next
            (routing/goto!  (str (string/join "/" (:path state)) "/" (:resource next) ) )
            nil

            )
          )
        )
      )


(defn key-pressed! [key state]
  (case key
    37 (left! state)
    39 (right! state)
    38 (up! state)
    40 (down! state)
    nil
  )
)
