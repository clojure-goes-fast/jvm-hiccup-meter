(ns jvm-hiccup-meter.core
  (:import jvm_hiccup_meter.MeterThread
           java.util.function.LongConsumer))

(defn start-hiccup-meter
  "Start the hiccup meter thread. It will continuosly sleep every `resolution-ms`
  milliseconds (10 by default) and call `callback-fn` with the presumed hiccup
  time in nanoseconds.

  Returns a nullary function which when called terminates the measuring thread."
  [callback-fn & {:keys [resolution-ms]}]
  (let [iw (MeterThread. (reify LongConsumer
                           (accept [_ v]
                             (callback-fn v)))
                      (or resolution-ms 10))]
    (.start iw)
    #(.terminate iw)))
