import { loadString } from 'nbb'

await loadString(`
  (require '[intuition.core :refer [main]])
  (main)
`)
