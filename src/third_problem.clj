(ns third-problem
  (:require [invoice-item :as ii]
            [clojure.test :refer [deftest is]]))

(deftest test-subtotal
  ;; first case, with discount rate of 0, spec 50 in subtotal
  (is (= (int (ii/subtotal {:invoice-item/precise-quantity 10
                            :invoice-item/precise-price 5
                            :invoice-item/discount-rate 0}))
         50))

  ;; second case, with discount of 50%, spec 25 in subtotal
  (is (= (int (ii/subtotal {:invoice-item/precise-quantity 10
                            :invoice-item/precise-price 5
                            :invoice-item/discount-rate 50}))
         25))

  ;; third case, with discount of 0, price 0, and quantity 0, spec 0 in subtotal
  (is (= (int (ii/subtotal {:invoice-item/precise-quantity 0
                            :invoice-item/precise-price 0}))
         0))

  ;; fourth case. with 1 item, spec 5 in subtotal, without discount
  (is (= (int (ii/subtotal {:invoice-item/precise-quantity 1
                            :invoice-item/precise-price 5}))
         5))

  ;; fifth case, with discount 100%, spec 0 in subtotal
  (is (= (int (ii/subtotal {:invoice-item/precise-quantity 10
                            :invoice-item/precise-price 5
                            :invoice-item/discount-rate 100}))
         0)))