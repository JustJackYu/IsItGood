import { validatePreferencesPatch } from "../src/services/preferences";

const ok = (r: ReturnType<typeof validatePreferencesPatch>) => {
    if ("error" in r) throw new Error("expected success, got error: " + r.error);
    return r.data;
};

describe("validatePreferencesPatch", () => {
    it("accepts a valid partial patch and returns only provided fields", () => {
        const data = ok(validatePreferencesPatch({ tone: "CRITICAL", lookOutFor: ["Action", "Story"] }));
        expect(data).toEqual({ tone: "CRITICAL", lookOutFor: ["Action", "Story"] });
    });

    it("rejects an invalid enum value", () => {
        expect(validatePreferencesPatch({ tone: "SARCASTIC" })).toEqual({ error: "Invalid tone" });
        expect(validatePreferencesPatch({ dealDisplay: "CHEAP" })).toEqual({ error: "Invalid dealDisplay" });
    });

    it("rejects wrong types", () => {
        expect(validatePreferencesPatch({ lookOutFor: "Action" }))
            .toEqual({ error: "lookOutFor must be an array of strings" });
        expect(validatePreferencesPatch({ chatLeaveWarning: "yes" }))
            .toEqual({ error: "chatLeaveWarning must be a boolean" });
    });

    it("allows null thresholds (clearing the alert)", () => {
        const data = ok(validatePreferencesPatch({ saleAlertDiscount: null, saleAlertPrice: null }));
        expect(data).toEqual({ saleAlertDiscount: null, saleAlertPrice: null });
    });

    it("bounds the discount threshold to 0..100", () => {
        expect(validatePreferencesPatch({ saleAlertDiscount: 150 }))
            .toEqual({ error: "saleAlertDiscount must be null or a number between 0 and 100" });
        expect(ok(validatePreferencesPatch({ saleAlertDiscount: 50 }))).toEqual({ saleAlertDiscount: 50 });
    });

    it("rejects a negative price threshold", () => {
        expect(validatePreferencesPatch({ saleAlertPrice: -5 }))
            .toEqual({ error: "saleAlertPrice must be null or a non-negative number" });
    });

    it("returns an empty patch for an empty body", () => {
        expect(ok(validatePreferencesPatch({}))).toEqual({});
    });
});
