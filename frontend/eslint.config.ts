// @ts-ignore
import js from "@eslint/js";
// @ts-ignore
import globals from "globals";
// @ts-ignore
import tseslint from "typescript-eslint";
// @ts-ignore
import pluginReact from "eslint-plugin-react";
import { defineConfig } from "eslint/config";

export default defineConfig([
  { files: ["**/*.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"], plugins: { js }, extends: ["js/recommended"], languageOptions: { globals: globals.browser } },
  tseslint.configs.recommended,
  pluginReact.configs.flat.recommended,
]);
