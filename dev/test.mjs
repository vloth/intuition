import { loadString, loadFile } from 'nbb'
import { readdir } from 'fs/promises';
import { join } from 'path';

const ends = (end) => (file) => file.endsWith(end)

async function loadFiles(dir, { filter, log }) {
  const files = (await readdir(dir, { recursive: true }))
    .filter(filter)

  for (const file of files) {
    await loadFile(join(dir, file))
    if (log) console.log(file + ' loaded')
  }
}

await loadFiles('./test', { filter: ends('_test.cljs'), log: false })
await loadString(`
  (require '[cljs.test :refer [run-all-tests]])
  (run-all-tests #".*intuition.*-test$")
`)

