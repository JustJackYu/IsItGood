/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: "ts-jest",
  testEnvironment: "node",
  testMatch: ["**/tests/**/*.test.ts"],
  transform: {
    "^.+\\.ts$": [
      "ts-jest",
      // The base tsconfig sets types: [] and strict flags; enable jest/node globals for tests.
      { tsconfig: { types: ["jest", "node"], esModuleInterop: true, noUnusedLocals: false, ignoreDeprecations: "6.0" } },
    ],
  },
};
