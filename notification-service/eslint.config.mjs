import js from "@eslint/js";
import globals from "globals";

export default [
  js.configs.recommended,

  // Configuración general Node
  {
    files: ["**/*.js"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "commonjs",
      globals: globals.node,
    },
  },

  // 🔥 Configuración específica para tests
  {
    files: ["**/__tests__/**/*.js", "**/*.test.js"],
    languageOptions: {
      globals: {
        ...globals.node,
        ...globals.jest, // 👈 AQUÍ está la solución
      },
    },
  },
];
