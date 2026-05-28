interface Game {
    id: number;
    name: string;
    background_image: string;
    rating: number;
    released: string;
}

const searchGames = async (query: string): Promise<Game[]> => {
    const apiKey = process.env.RAWG_API_KEY;
    if (!apiKey) {
        throw new Error("RAWG API Key is missing");
    }

    const searchUrl = `https://api.rawg.io/api/games?key=${apiKey}&search=${encodeURIComponent(query)}&page_size=10`;
    const response = await fetch(searchUrl);

    if (!response.ok) {
        throw new Error(`Failed to search games: ${response.statusText}`);
    }

    const data = await response.json();

    return data.results as Game[];
}

export { searchGames };