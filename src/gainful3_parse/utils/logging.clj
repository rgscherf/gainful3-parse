(ns gainful3-parse.utils.logging)

(defn log
  [printable]
  (println printable))

(defmacro try-log
  [tag call]
  `(try ~call
        (catch Exception e# ("Exception when calling "
                             '~call
                             " with objective "
                             ~tag
                             ", error msg was "
                             (.getMessage e#)))))

