import { loadString } from 'nbb'

await loadString(`
  (require '[nbb.nrepl-server :as nrepl]
           '[nbb.core :as core])
  (set! (.-await js/globalThis) core/await)
  (nrepl/start-server!)
`)
