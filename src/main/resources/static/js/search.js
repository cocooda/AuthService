const API_BASE = "http://localhost:7001/api";  // Change to production URL when deployed

async function submitSearch() {
  const query = document.getElementById("queryInput").value;

  const response = await fetch(`${API_BASE}/get_cached_result`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ query })
  });

  const data = await response.json();
  const resultDiv = document.getElementById("results");
  resultDiv.innerHTML = "";

  if (response.ok) {
    data.data.forEach(article => {
      const articleDiv = document.createElement("div");
      articleDiv.innerHTML = `
        <h3>${article.title}</h3>
        <p>${article.summary || ""}</p>
        <button onclick="saveArticle('${article.url}')">💾 Save</button>
        <button onclick="voteArticle('${article.url}', 1)">👍 Upvote</button>
        <button onclick="voteArticle('${article.url}', -1)">👎 Downvote</button>
        <hr>
      `;
      resultDiv.appendChild(articleDiv);
    });
  } else {
    resultDiv.innerHTML = `<p style="color:red;">${data.error}</p>`;
  }
}

async function saveArticle(url) {
  await fetch(`${API_BASE}/save`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ url })
  });
  alert("Article saved!");
}

async function voteArticle(url, vote_type) {
  await fetch(`${API_BASE}/vote`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ url, vote_type })
  });
  alert(vote_type === 1 ? "Upvoted!" : "Downvoted!");
}
