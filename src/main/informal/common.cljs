(ns informal.common
  (:require [reagent.core :as r]))

(defn event->value [event]
  (-> event .-target .-value))

(defn event->checked [event]
  (-> event .-target .-checked))

(defn merge-recursive [x y]
  (if (map? x)
    (merge x y)
    y))

(defn keyword->label [kwd]
  (when kwd
    (-> (name kwd)
      (clojure.string/replace #"[_-]" " ")
      clojure.string/capitalize)))

(defn field->label [field]
  (if (vector? field)
    (keyword->label (last field))
    (keyword->label field)))

(defn create-cursor [state field]
  (when field
    (cond
      (vector? field) (r/cursor state field)
      (keyword? field) (r/cursor state [field]))))

(defn tr [label]
  (name label))

(defn resolve-label [field label]
  (if label
    (if (keyword? label)
      (tr label)
      label)
    (field->label field)))

(defn vector-or-seq [coll]
  (or (vector? coll) (seq? coll)))

(defn realize-seq [colls s]
  (if (seq? s)
    (into colls (doall s))
    (conj colls s)))

(defn realize-seqs [seqs]
  (reduce realize-seq [] seqs))

(defn- branch? [node]
  (or (map? node) (sequential? node)))

(defn- children [node]
  (cond
    (map? node) (vals node)
    (sequential? node) node))

(defn some-not-nil [coll]
  (->> coll
    (tree-seq branch? children)
    (filter (complement branch?))
    (some identity)))
