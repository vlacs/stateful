(ns stateful
  (:require [clojure.edn]))

(def system-object (atom nil))

(defn get-system-object
  ([]
   (get-system-object system-object))
  ([mutable]
   (identity mutable)))

(defn set-state!
  ([new-value]
   (set-state! (get-system-object) new-value))
  ([mutable new-value]
   (reset! mutable new-value)))

(defn get-state
  ([]
   (get-state (get-system-object)))
  ([mutable]
   @mutable))

(defn put-in!
  ([path item]
   (put-in! (get-system-object) path item))
  ([mutable path item]
   (swap! mutable assoc-in path item)))

(defn get-from
  ([path]
   (get-from (get-system-object) path))
  ([mutable path]
   (get-in (get-state mutable) path)))

(defn transition!
  ([transition-fn]
   (transition! (get-system-object) transition-fn))
  ([mutable transition-fn]
   (set-state! mutable (transition-fn (get-state mutable)))))

(defn load-config!
  ([config-file-path]
   (load-config! [:config] config-file-path))
  ([path config-file-path]
   (load-config! (get-system-object) path config-file-path))
  ([mutable path config-file-path]
   (when-let [edn-str (slurp config-file-path)]
     (put-in!
       mutable path
       (clojure.edn/read-string edn-str)))))

(defn init!
  ([]
   (init! {}))
  ([initial-state]
   (init! (get-system-object) initial-state))
  ([mutable initial-state]
   (set-state! mutable initial-state)
   mutable))

(def start! transition!)
(def stop! transition!)

(defn destroy!
  ([]
   (destroy! (get-system-object)))
  ([mutable]
   (set-state! mutable nil)))
