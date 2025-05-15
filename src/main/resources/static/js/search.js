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
        <button onclick="voteUp('${article.url}')">👍 Upvote</button>
        <button onclick="voteDown('${article.url}')">👎 Downvote</button>
        <hr>
      `;
      resultDiv.appendChild(articleDiv);
    });
  } else {
    resultDiv.innerHTML = `<p style="color:red;">${data.error}</p>`;
  }
}

async function saveArticle(url) {
  try {
    const res = await fetch(`${API_BASE}/save`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ url })
    });

    const result = await res.json();
    alert(res.ok ? "Article saved!" : result.error || "Failed to save article");
  } catch (err) {
    console.error(err);
    alert("Error saving article.");
  }
}

async function voteUp(url) {
  try {
    const res = await fetch(`${API_BASE}/get_up_vote`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ url })
    });

    const data = await res.json();

    if (res.ok) {
      switch (data.vote_type) {
        case 1: alert("Upvoted!"); break;
        case -1: alert("Switched from downvote to upvote"); break;
        case 0: alert("Removed upvote"); break;
        default: alert("Vote registered");
      }
    } else {
      alert(data.error || "Error upvoting article.");
    }
  } catch (err) {
    console.error(err);
    alert("An error occurred during upvote.");
  }
}

async function voteDown(url) {
  try {
    const res = await fetch(`${API_BASE}/get_down_vote`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ url })
    });

    const data = await res.json();

    if (res.ok) {
      switch (data.vote_type) {
        case -1: alert("Downvoted!"); break;
        case 1: alert("Switched from upvote to downvote"); break;
        case 0: alert("Removed downvote"); break;
        default: alert("Vote registered");
      }
    } else {
      alert(data.error || "Error downvoting article.");
    }
  } catch (err) {
    console.error(err);
    alert("An error occurred during downvote.");
  }
}