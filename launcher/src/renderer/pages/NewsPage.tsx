import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../styles/NewsPage.css';

interface NewsPost {
  id: string;
  title: string;
  content: string;
  author: string;
  publishedAt: string;
  imageUrl?: string;
  isPinned: boolean;
}

const NewsPage: React.FC = () => {
  const [news, setNews] = useState<NewsPost[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNews();
  }, []);

  const fetchNews = async () => {
    try {
      const config = await window.api.getConfig();
      const response = await axios.get(`${config.apiUrl}/api/news`);
      setNews(response.data.news);
    } catch (error) {
      console.error('Failed to fetch news:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading news...</div>;
  }

  return (
    <div className="news-page">
      <h1 className="page-title">Latest News</h1>

      <div className="news-list">
        {news.length === 0 ? (
          <div className="no-news">No news available yet.</div>
        ) : (
          news.map((post) => (
            <article key={post.id} className={`news-card ${post.isPinned ? 'pinned' : ''}`}>
              {post.imageUrl && (
                <img src={post.imageUrl} alt={post.title} className="news-image" />
              )}
              <div className="news-content">
                <h2 className="news-title">
                  {post.isPinned && <span className="pin-icon">📌</span>}
                  {post.title}
                </h2>
                <p className="news-meta">
                  By {post.author} • {new Date(post.publishedAt).toLocaleDateString()}
                </p>
                <p className="news-text">{post.content}</p>
              </div>
            </article>
          ))
        )}
      </div>
    </div>
  );
};

export default NewsPage;
