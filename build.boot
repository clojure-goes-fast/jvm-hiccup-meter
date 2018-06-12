(task-options!
 pom {:project     'com.clojure-goes-fast/jvm-hiccup-meter
      :version     "0.1.1"
      :description "Measure JVM and OS hiccups influencing your program"
      :url         "http://github.com/clojure-goes-fast/jvm-hiccup-meter"
      :scm         {:url "http://github.com/clojure-goes-fast/jvm-hiccup-meter"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 push {:repo "clojars"})

(set-env! :source-paths #{"src"})

(deftask build []
  (comp (javac)
        (sift :to-resource [#"jvm_hiccup_meter/.+\.clj$"])
        (pom) (jar)))

(comment ;; Development
  (set-env! :dependencies #(conj % '[virgil "LATEST"]))
  (require '[virgil.boot :as virgil])
  (boot (virgil/javac* :verbose true)))
