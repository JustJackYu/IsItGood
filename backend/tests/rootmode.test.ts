import { detectRootMode } from "../src/services/gemini";

describe("detectRootMode", () => {
    it("is inactive for a normal message", () => {
        const r = detectRootMode("Is this game good?");
        expect(r.active).toBe(false);
        expect(r.message).toBe("Is this game good?");
    });

    it("activates and strips the trigger word", () => {
        const r = detectRootMode("rootmode what is 2+2?");
        expect(r.active).toBe(true);
        expect(r.message).toBe("what is 2+2?");
    });

    it("is case-insensitive and tolerates leading whitespace", () => {
        const r = detectRootMode("   RootMode ping");
        expect(r.active).toBe(true);
        expect(r.message).toBe("ping");
    });

    it("falls back to a confirmation prompt when only the trigger is sent", () => {
        const r = detectRootMode("rootmode");
        expect(r.active).toBe(true);
        expect(r.message).toBe("Confirm that root mode is active.");
    });

    it("does not trigger when the word appears mid-message", () => {
        const r = detectRootMode("does it have a rootmode feature?");
        expect(r.active).toBe(false);
    });
});
