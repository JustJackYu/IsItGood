interface TavilyResult {
    title: string;
    url: string;
    content: string;
    score: number;
    raw_content?: string;
}

const searchGameReviews = async (query: string): Promise<Array<{ content: string; url: string }>> => {
    const apiKey = process.env.TAVILY_API_KEY;
    if (!apiKey) {
        throw new Error("TAVILY API Key is missing");
    }

    const searchUrl = `https://api.tavily.com/search`;
    const options: RequestInit = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            api_key: apiKey,
            query: `${query} game review`,
            max_results: 5,
        })
    };
    const response = await fetch(searchUrl, options);

    if (!response.ok) {
        throw new Error(`Failed to search games: ${response.statusText}`);
    }

    const data = await response.json();
    const results = data.results as TavilyResult[];

    return results.map(result => ({ content: result.content, url: result.url }));
}

const searchWebContext = async (query: string): Promise<Array<{ content: string; url: string }>> => {
    const TAVILY_API_KEY = process.env.TAVILY_API_KEY;

    if (!TAVILY_API_KEY) {
        throw new Error("Tavily API Key is missing");
    }

    const response = await fetch("https://api.tavily.com/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            api_key: TAVILY_API_KEY,
            query,
            max_results: 3,
            search_depth: "basic"
        })
    });

    const data = await response.json();
    return (data.results ?? []).map((r: any) => ({
        content: r.content,
        url: r.url
    }));
};

export { searchGameReviews, searchWebContext };