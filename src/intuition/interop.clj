(ns intuition.interop
  (:import
   [java.time ZoneId LocalTime DayOfWeek]
   [java.time.format DateTimeFormatter]
   [java.time.temporal ChronoField]))

(defn now []
  (java.util.Date/from (java.time.Instant/now)))

(defn date->localdatetime
  ([value]
   (date->localdatetime value (ZoneId/of "UTC")))
  ([value zone-id]
   (-> value
       (.toInstant)
       (.atZone zone-id)
       (.toLocalDateTime))))

(defn localdatetime->time
  ([local-date-time]
   (localdatetime->time local-date-time (ZoneId/of "UTC")))
  ([local-date-time zone-id]
   (-> local-date-time
       (.atZone zone-id)
       (.toInstant)
       (.toEpochMilli))))

(defn localdatetime->str
  [localdatetime str-format]
  (.format localdatetime (DateTimeFormatter/ofPattern str-format)))

(defn weekend?
  [localdatetime]
  (contains?
   #{DayOfWeek/SATURDAY DayOfWeek/SUNDAY}
   (DayOfWeek/of (.get localdatetime ChronoField/DAY_OF_WEEK))))

(defn subtract-days
  [date days]
  (.minusDays date days))

(defn iterate-days [start]
  (iterate #(.plusDays % 1) start))

(defn at-midnight [date]
  (-> date
      (.toLocalDate)
      (.atTime LocalTime/MIDNIGHT)))
