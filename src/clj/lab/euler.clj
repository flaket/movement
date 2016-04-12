(ns lab.euler)

; Problem 4 - Largest palindrome product
; A palindromic number reads the same both ways. The largest palindrome made from the product of two 2-digit numbers is 9009 = 91 × 99.
;Find the largest palindrome made from the product of two 3-digit numbers.
(time
  ; Dette virker som en ålreit brute force løsning som finner løsningen på 250ms. Det finnes sikkert noen triks så man slipper å søke gjennom alle produktene.
  (let [results (for [x (range 1000) y (range 1000)
                      :let [product (* x y)]
                      :when (= (str product) (clojure.string/reverse (str product)))]
                  product)]
    (reduce max results)))

; Problem 5 - Smallest multiple
; 2520 is the smallest number that can be divided by each of the numbers from 1 to 10 without any remainder.
; What is the smallest positive number that is evenly divisible by all of the numbers from 1 to 20?

; Første forsøk, brute force som finner løsning på 35s.
#_(time (some (fn [num] (when (and (not (zero? num)) (not (ratio? (/ num 20))) (not (ratio? (/ num 19))) (not (ratio? (/ num 18)))
                                 (not (ratio? (/ num 17))) (not (ratio? (/ num 16))) (not (ratio? (/ num 15))) (not (ratio? (/ num 14)))
                                 (not (ratio? (/ num 13))) (not (ratio? (/ num 12))) (not (ratio? (/ num 11))) (not (ratio? (/ num 10)))
                                 (not (ratio? (/ num 9))) (not (ratio? (/ num 8))) (not (ratio? (/ num 7))) (not (ratio? (/ num 6)))
                                 (not (ratio? (/ num 5))) (not (ratio? (/ num 4))) (not (ratio? (/ num 3))) (not (ratio? (/ num 2)))) num)) (range)))
; Andre forsøk: mer elegant kode, men fremdeles brute force og kjøre tid på > 30 sek.
#_(time (->> (for [x (range) :let [y (map #(rem x %) (range 11 21))] :when (every? zero? y)] [x y]) (take 2) last first))

(defn gcd
  "Euclid's algoritme for greatest common divisor, gitt at både a og b er positive.
  https://en.wikipedia.org/wiki/Greatest_common_divisor"
  [a b]
  (cond
    (= a b) a
    (> a b) (recur (- a b) b)
    (< a b) (recur a (- b a))))

(defn lcm
  "Least common multiple, ved reduksjon av gcd.
  https://en.wikipedia.org/wiki/Least_common_multiple#Computing_the_least_common_multiple"
  [a b]
  (/ (* a b) (gcd a b)))

(defn smallest-multiple
  "Returns the smallest positive number that is evenly divisible by all the numbers from 1 to number"
  [number]
  (reduce #(lcm %2 %1) (range 1 number)))

#_(time (smallest-multiple 24))

