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

;; print info of invoices to validate in console (per item)
(doseq [item (:invoice/items invoice)]
  ;; Item information
  (println "Item ID:" (:invoice-item/id item))
  (println "Item SKU:" (:invoice-item/sku item))
  (doseq [tax (:taxable/taxes item)]
    ;: tax per item information
    (println "Item Tax ID:" (:tax/id tax))
    (println "Item Tax Category:" (:tax/category tax))
    (println "Item Tax Rate:" (:tax/rate tax))
    )
  (doseq [retention (:retentionable/retentions item)]
    ;: Retention per item information
    (println "Item Retention ID:" (:retention/id retention))
    (println "Item Retention Category:" (:retention/category retention))
    (println "Item Retention Rate:" (:retention/rate retention))
    )
  )


;; match iva, iva category, tax rate 19% in taxable/taxes list
(defn match-iva [item]
  (some #(and (= (:tax/category %) :iva)
              (= (:tax/rate %) 19))
        (:taxable/taxes item)))

;; match retention, ret_fuente category, retantion rate 1% in retentionable/retentions list
(defn match-retention [item]
  (some #(and (= (:retention/category %) :ret_fuente)
              (= (:retention/rate %) 1))
        (:retentionable/retentions item)))


;; function to filter items in invoice
(defn validItems [invoice]
  (let [items (:invoice/items invoice)
        ;; Filter to items with respective functions
        filtered-items (filter #(or
                                  ;; verify match iva and not match retention
                                  (and (match-iva %) (not (match-retention %)))
                                  ;; verify match retention and not match iva
                                  (and (match-retention %) (not (match-iva %))))
                               items)]
    ;; verify functions have return items to iva and retention
    (if (and (some match-iva filtered-items)
             (some match-retention filtered-items))
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
