(ns gainful3-parse.utils.logging
  (:require [clojure.tools.logging :as log]))

(defn info
  [printable]
  (log/info printable))

(defn warn
  [printable]
  (log/warn printable))

(defmacro try-log
  [tag call]
  `(try ~call
        (catch Exception e# (log/warn (str "Exception when calling "
                                           '~call
                                           " with objective '"
                                           ~tag
                                           "', error msg was "
                                           (.getMessage e#))))))

