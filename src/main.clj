(ns main
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

;; Call invoice to import information
(def invoice
  (->> "invoice.edn"
       io/resource
       slurp
       edn/read-string))

;; ID of invoice
(println "Invoice ID: " (:invoice/id invoice))

;; match iva, iva category, tax rate 19% in taxable/taxes list
(defn match-iva [item]
  (->> (:taxable/taxes item)
       (some #(and (= (:tax/category %) :iva)
                   (= (:tax/rate %) 19)))))

;; match retention, ret_fuente category, retantion rate 1% in retentionable/retentions list
(defn match-retention [item]
  (->> (:retentionable/retentions item)
       (some #(and (= (:retention/category %) :ret_fuente)
                   (= (:retention/rate %) 1)))))


;; function to filter items in invoice
(defn validItems [invoice]
  (let [filtered-items (->> (:invoice/items invoice)
                            ;; Filter to items with respective functions
                            (filter #(or
                                       ;; verify match iva and not match retention
                                       (and (match-iva %) (not (match-retention %)))
                                       ;; verify match retention and not match iva
                                       (and (match-retention %) (not (match-iva %))))))]
    ;; verify functions have return items to iva and retention
    (if (and (->> filtered-items (some match-iva))
             (->> filtered-items (some match-retention)))
      ;; if true, return filtered list
      filtered-items
      ;; if false, return empty list
      [])))

;; print in console result
(print (validItems invoice))

;; print in txt file filan resutl
(let [finalItemList (validItems invoice)]
  (with-open [wtr (io/writer "invoice-filtered.txt")]
    (.write wtr (clojure.string/join "\n" finalItemList))
    ))
