(ns second-problem
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [invoice-spec :as spec])
  (:import [java.time LocalDate ZoneId]
           [java.time.format DateTimeFormatter]))

; get info of json invoice
(def invoice
  (json/read-str (slurp (io/resource "invoice.json")) :key-fn keyword))

;; function to transform date to timestamps and return then
(defn transform-date-parse [date-str]
  ; defined formatter to match date in format "dd/MM/yyyy"
  (let [formatter (DateTimeFormatter/ofPattern "dd/MM/yyyy")
        ; parse date with formatter
        local-date (LocalDate/parse date-str formatter)
        ; convert to instant with utf
        instant (.atStartOfDay local-date (ZoneId/of "UTC"))] ;; Assume want will return date in UTF format
    ;  result with instant
    (.toInstant instant)))

; function to transform clojure object to spect data to match
(defn transform-invoice-object [data]
  (let [invoice (:invoice data)
        ; get and transform date
        issue-date (transform-date-parse (:issue_date invoice))
        ; map and validate items
        items (vec (map (fn [item]
                          {:invoice-item/sku (:sku item)
                           ; transform tax category string to keyword of clojure (keyword is a symbol, unique and immutable) and transform to lower case
                           :invoice-item/taxes [{:tax/category (keyword (str/lower-case (:tax_category (first (:taxes item)))))
                                                 ; extract value from tax_rate and transform to double
                                                 :tax/rate (double (:tax_rate (first (:taxes item))))}]
                           ; extract info of price from element or item
                           :invoice-item/price (:price item)
                           ; extract info of quality from element of item
                           :invoice-item/quantity (:quantity item)})
                        ; return items mapped from invoice
                        (:items invoice)))]

    ; return data formatted with previous transformed data
    {:invoice/issue-date issue-date
     :invoice/customer {:customer/name (:company_name (:customer invoice))
                        :customer/email (:email (:customer invoice))}
     :invoice/items items}))

; final transform object invoice
(def transformed-invoice (transform-invoice-object invoice))

; valid object invoice is valid
(println "invoice object is valid?" (s/valid? ::spec/invoice transformed-invoice))

;; verify if not is valid, where have error
(if (not (s/valid? ::spec/invoice transformed-invoice))
  (s/explain ::spec/invoice transformed-invoice))