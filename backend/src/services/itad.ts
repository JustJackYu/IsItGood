interface Deal {
    store: string,
    price: number,
    currency: string,
    url: string
}

const getGamePrices = async (query: string): Promise<Deal[]> => {
    // 1. Search for the game ID within ITAD service
    const API_KEY = process.env.ITAD_API_KEY;
    if (!API_KEY) {
        throw new Error("ITAD API Key is missing");
    }

    const BASE = 'https://api.isthereanydeal.com';

    const searchResponse = await fetch(
        `${BASE}/games/search/v1?title=${encodeURIComponent(query)}&key=${API_KEY}`
    );
    const searchData = await searchResponse.json() as any[];

    console.log(searchData)

    if (!searchData || searchData.length === 0) {
        console.log("No game found")
        return [];
    }

    // 2. Get current prices for that game ID
    const gameId = searchData[0].id;

    const priceResponse = await fetch(
        `${BASE}/games/prices/v3?key=${API_KEY}&country=CA`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify([gameId]),
        }
    );

    const priceData = await priceResponse.json() as any[];

    console.log(priceData);

    const deals = priceData[0]?.deals ?? [];

    return deals.map((d: any) => ({
        store: d.shop.name,
        price: d.price.amount,
        currency: d.price.currency,
        url: d.url,
    }));
}

export { getGamePrices };