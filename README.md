# jvm-hiccup-meter

A miniature library for measuring JVM and OS hiccups that affect your program.
It is inspired by Gil Tene's [jHiccup](https://github.com/giltene/jHiccup). In
fact, this is a maximally trimmed jHiccup with the implementation taken straight
out of the ancestor library.

jHiccup is a great tool that focuses on very precise on-demand measurement of
platform-induced lags. It uses
[HdrHistogram](https://github.com/HdrHistogram/HdrHistogram) to record
high-fidelity measurement data which can later be queried for real percentile
values on arbitrary time spans. However, jHiccup's API is not convenient for
ongoing always-on measuring and reporting.

jvm-hiccup-meter contains the exact measuring code from jHiccup and exposes it
via the most primitive callback API. This gives developers the utmost
flexibility in consuming the measurements, be it writing them to a monitoring
solution (Metrics, StatsD, etc.) or aggregating in any other way.

## How it works

The idea behind jHiccup (and, as a consequence, this library) is trivially
brilliant: if an idle thread that does nothing but sleeping is denied the CPU
time by the system, then the real worker threads are likely to observe the
similar effect. jvm-hiccup-meter starts a thread that sleeps for a defined
interval (by default, 10 milliseconds) and measures how much of actual time has
passed. The difference between the desired and actual sleep time becomes the
length of the system "hiccup" for this cycle.

Of course, you can't expect `Thread.sleep(10)` to wake up strictly in 10
milliseconds even under ideal conditions when no hiccups are happening. But
obsessing about small fluctuations is not the point, the point is to discover
long pauses of hundreds of milliseconds or even seconds. Those can be caused by
stop-the-world GC phases, stolen CPU cycles in virtualized environments,
unpredicted hardware effects, and other application-independent reasons.

jvm-hiccup-meter is non-invasive — an extra thread that does a few arithmetic
operations and goes to sleep a hundred times per second should not bring a
noticeable overhead to your application.

## Usage

[![](https://clojars.org/com.clojure-goes-fast/jvm-hiccup-meter/latest-version.svg)](https://clojars.org/com.clojure-goes-fast/jvm-hiccup-meter)

```clj
(require '[clj-hiccup-meter.core :as hmeter])
(def hm (hmeter/start-hiccup-meter callback-fn :resolution-ms 10))

;; To stop the meter thread
(hm)
```

The only function `start-hiccup-meter` accepts an unary function as an argument
and starts the measuring thread. On each measurement iteration, the callback
will be called with the measured delay in nanoseconds. Optional resolution can
be passed to change the default sleep period. `start-hiccup-meter` returns a
nullary function calling which stops the thread.

Here's an example of combining jvm-hiccup-meter with [Dropwizard
Metrics](https://github.com/dropwizard/metrics):

```clj
(import 'com.codahale.metrics.Histogram
        'com.codahale.metrics.SlidingTimeWindowArrayReservoir
        'java.util.concurrent.TimeUnit)

(def hist (Histogram. (SlidingTimeWindowArrayReservoir. 10 TimeUnit/SECONDS)))
(def hm (hmeter/start-hiccup-meter #(.update ^Histogram hist %)))

;; Now, you can forward this histogram to Graphite, or check the values
;; manually, e.g:
(.getMax (.getSnapshot hist))
```

## License

jHiccup was written by Gil Tene of Azul Systems, and released to the public
domain, as explained at http://creativecommons.org/publicdomain/zero/1.0/

---

jvm-hiccup-meter is distributed under the Eclipse Public License. See
[LICENSE](LICENSE).

Copyright 2018 Alexander Yakushev
