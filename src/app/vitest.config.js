import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'
import { configDefaults, defineConfig } from 'vitest/config'
import TrevorismTestResultReporter from '@trevorism/vitest-test-result-events'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    exclude: [...configDefaults.exclude, 'e2e/*', 'cypress/*'],
    root: fileURLToPath(new URL('./', import.meta.url)),
    reporters: ['default', new TrevorismTestResultReporter('login')]
  }
})
