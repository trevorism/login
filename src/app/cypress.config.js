import { defineConfig } from "cypress";
import registerTrevorismEventSender from "@trevorism/cypress-test-result-events";

export default defineConfig({

  allowCypressEnv: false,

  e2e: {
    baseUrl: "https://login.auth.trevorism.com",
    setupNodeEvents(on, config) {
      registerTrevorismEventSender({on, config}, 'login');
    }
  },

  component: {
    devServer: {
      framework: "vue",
      bundler: "vite",
    },
  },
});
