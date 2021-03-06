;;; Horibble BUG!!!!!!!!!!!!!!!!
(ns mcmc-sampling.core
  (:require [mcmc-sampling.util :as util]
            [mcmc-sampling.gibbs :as gibbs]
            [clojure.core.matrix :as m])
  (:gen-class))

(def mu1 [0 0])
(def mu2 [2 2])
(def a1 [[1 0] [0 1]])
(def a2 [[1 0] [0 1]])
(def number-of-samples 1000000)
(def d 0.1)
(def area-of-d (* Math/PI (util/sqr d)))

(def f1 (partial util/gauss-function-2d mu1 a1))
(def f2 (partial util/gauss-function-2d mu2 a2))

(defn multi-gauss-function
  "Returns the average of two gauss functions. "
  [x]
  (/ (+ (f1 x) (f2 x)) 2))

(def density-function multi-gauss-function)

(defn get-random-sample
  []
  (m/add (util/gauss-sample-2d) [1 1]))

(defn sample-density-function
  [point]
  (util/standard-gauss-function-2d
   (m/add point [-1 -1])))

(defn- mh-get-next-sample
  "Get next sample in MH algorithm"
  [previous-sample]
  (let [new-sample (get-random-sample)
        threshold (/ (* (sample-density-function previous-sample)
                        (density-function new-sample))
                     (* (sample-density-function new-sample)
                        (density-function previous-sample)))
        alpha (min threshold 1)
        u (util/uniform-sample)]
    (if (<= u alpha)
      new-sample
      previous-sample)))

(defn- mh-get-samples
  "Get samples using MH algorithm."
  [f]
  (loop [samples [[0 0]]
         i 0]
    (if (= i number-of-samples)
      samples
      (recur (conj samples
                   (mh-get-next-sample (peek samples)))
             (inc i)))))

(defn- mc-calc-probability-density
  "Calc probability density of point x using Monte Carlo Method."
  [x]
  (let [samples (mh-get-samples density-function)]
    (loop [i 0
           cnt 0]
      (if (= i number-of-samples)
        (/ (/ cnt number-of-samples)
           area-of-d)
        (recur (inc i)
               (+ cnt (util/neighbor? x (samples i) d)))))))

(defn- output
  [samples]
  (reduce #(str %1 "\n" %2)
          (map #(str (first %) " " (second %))
               samples)))

(defn- run
  []
  (println (mc-calc-probability-density [0.1 0.1]))
  )

(defn- print-to-file
  []
  (let [mh-sample (mh-get-samples density-function)]
    (println "IO Stage")
    (spit "mh.output" mh-sample)
    ))

(def -main run)
