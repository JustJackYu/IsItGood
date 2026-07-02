import prisma from "../prisma/client";

export interface UserPreferences {
    summaryLength: "SHORT" | "MEDIUM" | "LONG";
    tone: "CASUAL" | "BALANCED" | "CRITICAL" | "ENTHUSIASTIC";
    lookOutFor: string[];
    allowMatureContent: boolean;
    fontSize: "SMALL" | "MEDIUM" | "LARGE";
    dealDisplay: "PRICE" | "DISCOUNT" | "BOTH";
    saleAlertDiscount: number | null;
    saleAlertPrice: number | null;
    chatLeaveWarning: boolean;
}

export const SUMMARY_LENGTHS: UserPreferences["summaryLength"][] = ["SHORT", "MEDIUM", "LONG"];
export const TONES: UserPreferences["tone"][] = ["CASUAL", "BALANCED", "CRITICAL", "ENTHUSIASTIC"];
export const FONT_SIZES: UserPreferences["fontSize"][] = ["SMALL", "MEDIUM", "LARGE"];
export const DEAL_DISPLAYS: UserPreferences["dealDisplay"][] = ["PRICE", "DISCOUNT", "BOTH"];

export const DEFAULT_PREFERENCES: UserPreferences = {
    summaryLength: "MEDIUM",
    tone: "BALANCED",
    lookOutFor: [],
    allowMatureContent: false,
    fontSize: "MEDIUM",
    dealDisplay: "BOTH",
    saleAlertDiscount: null,
    saleAlertPrice: null,
    chatLeaveWarning: true,
};

// Validates a partial preferences update. Returns the sanitized fields to persist, or an error
// message. Pure (no I/O) so it can be unit-tested and reused by the route handler.
export function validatePreferencesPatch(
    body: any
): { data: Record<string, unknown> } | { error: string } {
    const data: Record<string, unknown> = {};
    const {
        summaryLength, tone, lookOutFor, allowMatureContent, fontSize,
        dealDisplay, saleAlertDiscount, saleAlertPrice, chatLeaveWarning,
    } = body ?? {};

    if (summaryLength !== undefined) {
        if (!SUMMARY_LENGTHS.includes(summaryLength)) return { error: "Invalid summaryLength" };
        data.summaryLength = summaryLength;
    }
    if (tone !== undefined) {
        if (!TONES.includes(tone)) return { error: "Invalid tone" };
        data.tone = tone;
    }
    if (fontSize !== undefined) {
        if (!FONT_SIZES.includes(fontSize)) return { error: "Invalid fontSize" };
        data.fontSize = fontSize;
    }
    if (dealDisplay !== undefined) {
        if (!DEAL_DISPLAYS.includes(dealDisplay)) return { error: "Invalid dealDisplay" };
        data.dealDisplay = dealDisplay;
    }
    if (lookOutFor !== undefined) {
        if (!Array.isArray(lookOutFor) || !lookOutFor.every((x: any) => typeof x === "string")) {
            return { error: "lookOutFor must be an array of strings" };
        }
        data.lookOutFor = lookOutFor;
    }
    if (allowMatureContent !== undefined) {
        if (typeof allowMatureContent !== "boolean") return { error: "allowMatureContent must be a boolean" };
        data.allowMatureContent = allowMatureContent;
    }
    if (chatLeaveWarning !== undefined) {
        if (typeof chatLeaveWarning !== "boolean") return { error: "chatLeaveWarning must be a boolean" };
        data.chatLeaveWarning = chatLeaveWarning;
    }
    if (saleAlertDiscount !== undefined) {
        if (saleAlertDiscount !== null &&
            (typeof saleAlertDiscount !== "number" || saleAlertDiscount < 0 || saleAlertDiscount > 100)) {
            return { error: "saleAlertDiscount must be null or a number between 0 and 100" };
        }
        data.saleAlertDiscount = saleAlertDiscount;
    }
    if (saleAlertPrice !== undefined) {
        if (saleAlertPrice !== null && (typeof saleAlertPrice !== "number" || saleAlertPrice < 0)) {
            return { error: "saleAlertPrice must be null or a non-negative number" };
        }
        data.saleAlertPrice = saleAlertPrice;
    }
    return { data };
}

// Returns the user's stored preferences, or sensible defaults if they haven't set any yet.
export const getEffectivePreferences = async (userId: number): Promise<UserPreferences> => {
    const prefs = await prisma.userPreferences.findUnique({ where: { userId } });
    if (!prefs) return DEFAULT_PREFERENCES;

    return {
        summaryLength: prefs.summaryLength,
        tone: prefs.tone,
        lookOutFor: prefs.lookOutFor,
        allowMatureContent: prefs.allowMatureContent,
        fontSize: prefs.fontSize,
        dealDisplay: prefs.dealDisplay,
        saleAlertDiscount: prefs.saleAlertDiscount,
        saleAlertPrice: prefs.saleAlertPrice,
        chatLeaveWarning: prefs.chatLeaveWarning,
    };
};
