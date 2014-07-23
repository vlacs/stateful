(ns stateful
  (:require [clojure.edn]))

(defn new-system-object
  "Returns a nil atom."
  []
  (atom nil)) 

(def system-object (new-system-object))

(defn get-system-object
  "If no argument is provided, the locally stored system will be returned.
  If an argument is provided, its identity is returned."
  ([]
   (get-system-object system-object))
  ([mutable]
   (identity mutable)))

(defn set-state!
  "Replaces the state of the system with a new object. If the mutable storage
  isn't provided, the local storage is assumed."
  ([new-value]
   (set-state! (get-system-object) new-value))
  ([mutable new-value]
   (reset! mutable new-value)))

(defn get-state
  "Gets the local system state or dereferences the state passed into the fn."
  ([]
   (get-state (get-system-object)))
  ([mutable]
   @mutable))

(defn put-in!
  "Alters the system object by adding a (potentially nested) item to the system
  map and returning the new system value."
  ([path item]
   (put-in! (get-system-object) path item))
  ([mutable path item]
   (swap! mutable assoc-in path item)))

(defn get-from
  "Gets an item in a nested system map. Basically a get-in wrapper. It uses the
  local system if one isn't provided."
  ([path]
   (get-from (get-system-object) path))
  ([mutable path]
   (get-in (get-state mutable) path)))

(defn transition!
  "Calls a fn with the current state of the system which returns a new system
  state that replaces the old value. The local system is used unless one is
  provided."
  ([transition-fn]
   (transition! (get-system-object) transition-fn))
  ([mutable transition-fn]
   (set-state! mutable (transition-fn (get-state mutable)))))

(defn load-config!
  "Loads an edn config file given a path. The local system is used if one isn't
  provided and the path in the system is assumed to be [:config] is one isn't
  provided. If you provide the mutable item, you must provide the path."
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
  "Initializes the system object. Defaults assume you want an empty map and that
  you're using the local system object, both options are overrided to provide
  defaults."
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
  "Invalidates the system state by setting it to nil."
  ([]
   (destroy! (get-system-object)))
  ([mutable]
   (set-state! mutable nil)))
