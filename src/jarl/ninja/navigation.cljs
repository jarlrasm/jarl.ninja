(ns ^:figwheel-always jarl.ninja.navigation
)

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

(defn get-direction-to [state page path]
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
