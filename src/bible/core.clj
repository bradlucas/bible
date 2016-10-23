(ns bible.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [cli]]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

;; The return from getbible.net is embedded in (..); and this is breaking the json parser
;;

(def url "http://getbible.net/json?v=kjv&p=titus")

(defn paren-wrapped? [str] (= (nth str 0) \())

(defn unwrap-parens
  "String is wrapped in (...);"
  [str]
  (subs str 1 (- (count str) 2)))

(defn process-json
  [str]
  (let [s (if (paren-wrapped? str) (unwrap-parens str) str)]
    (json/read-str s)))

(defn get-json-url
  [url]
  (let [page (client/get url)]
    (if page
      (process-json (page :body)))))

;; ----------------------------------------------------------------------------------------------------

(defn load-book-data
  [book-name]
  (let [result (get-json-url (format "http://getbible.net/json?v=kjv&p=%s" book-name))]
    (result "book")))

(defn get-book 
  [book-name]
  (load-book-data book-name))

(defn chapter-numbers
  "Return the chapter numbers for a given book"
  [book]
  (let [chapters book]
    (sort (map #(Integer/parseInt %) (keys chapters)))))

(defn get-chapter
  [book chapter-num]
  ((book (str chapter-num)) "chapter"))

(defn verse-numbers
  "Return the verse numbers for a given a chapter"
  [chapter]
  (sort (map #(Integer/parseInt %) (keys chapter))))

(defn verse
  "Return the verse for a chapter number"
  [chapter verse-num]
  ((chapter (str verse-num)) "verse"))

(defn verse-range
  [chapter verse-num1 verse-num2]
  (let [v1 (if (string? verse-num1) (Integer/parseInt verse-num1) verse-num1)
        v2 (if (string? verse-num2) (Integer/parseInt verse-num2) verse-num2)]
    (map #(verse chapter %) (range v1 (inc v2)))))

(defn verses
  "Return all the verse for a given chapter"
  [chapter]
  (let [nums (verse-numbers chapter)
        last-num (apply max nums)]
    (println nums)
    (println last-num)
    (verse-range chapter 1 last-num)))

(defn book-names
  "Return the books names from our book data file"
  []
  (slurp "doc/books.txt"))

;; ----------------------------------------------------------------------------------------------------
(defn third [x] (first (next (next x))))

;; if two args
;; parse chapter:verse-[verse]

(require '[clojure.string :refer [split]])

(defn parse-args
  [args]
  (let [book-name (first args)]
    (if (next args)
      (let [[chapter verse1] (split (second args) #":")]
        (if verse1 
          (let [[verse1 verse2] (split verse1 #"-")]
            (if verse2
              [book-name chapter verse1 verse2]
              [book-name chapter verse1 nil]
              )
            )
          [book-name chapter nil nil]))
      [book-name nil nil nil])))

;; (defn process-args
;;   [args]
;;   (let [num-params (count args)]
;;     (if (zero? num-params)
;;       (book-names)
;;       (let [book-name (first args)
;;             chapter-num (second args)
;;             verse-num (third args)
;;             book (get-book book-name)
;;             ]
;;         (if (empty? book)
;;           (println (format "Unable to find book '%s'" book-name)
;;                    "\n\n"
;;                    (book-names))
;;           (if (empty? chapter-num)
;;             (chapter-numbers book)
;;             (let [chapter (get-chapter book chapter-num)]
;;               (if (empty? verse-num)
;;                 (verse-numbers chapter)
;;                 (verse chapter verse-num)))))))))

(defn run-command
  [opts args banner]
  (let [[book-name chapter-num verse1 verse2] (parse-args args)]
    (println book-name chapter-num verse1 verse2)
    (if (and verse1 verse2)
      (do
        (println verse1 verse2)
        (verse-range (get-chapter (get-book book-name) chapter-num) verse1 verse2))
      (if verse1
        (verse (get-chapter (get-book book-name) chapter-num) verse1)
        (if chapter-num 
          (verse-numbers (get-chapter (get-book book-name) chapter-num))
          (chapter-numbers (get-book book-name))
          )
        )
      )
    )
)

(defn -main 
  [& args]
  (println args)
  (let [[opts args banner]
        (cli args
             ["-b" "--banner-help" "Show banner helphelp" :flag true :default false]
             ["-q" "--quit" "Quit aftter return" :flag true :default true]
             ["-f" "--output-file" "File to write response to"]             
             ["-t" "--text" "Return results in text format" :flag true :default true]
             ["-j" "--json" "Return results in json format" :flag true :default false]
             ["-x" "--xml" "Return results in xml format" :flag true :default false]
             ["-h" "--html" "Return results in html format" :flag true :default false])]
    (if (:banner-help opts)
      (println banner)
      (println (run-command opts args banner)))))

