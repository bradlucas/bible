(defproject bible "1.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-http "2.0.0"]
                 ;; c;j-http-lite does something odd and the server returns 403
                 ;; [clj-http-lite "0.3.0"]
                 ]
  :main bible.core
)
