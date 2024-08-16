import { loadString } from 'nbb'

await loadString(`
  (require '[core :refer [main]])
  (main)
`)
