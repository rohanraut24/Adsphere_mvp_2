import React from 'react';
import { Clock, MessageSquare } from 'lucide-react';

const ArticleCard = ({ title, excerpt, imageUrl, author, readTime }) => {
  return (
    <div className="glass-card animate-fade-in" style={{ padding: '0', overflow: 'hidden' }}>
      <img src={imageUrl} alt={title} className="article-image" style={{ margin: '0', borderRadius: '12px 12px 0 0' }} />
      
      <div style={{ padding: '24px' }}>
        <h3 className="article-title">{title}</h3>
        <p className="article-excerpt">{excerpt}</p>
        
        <div className="article-meta">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontWeight: '500', color: 'var(--text-primary)' }}>{author}</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Clock size={14} /> {readTime} min read
            </span>
          </div>
        </div>
        
        <div style={{ marginTop: '24px' }}>
          <a href="#" className="btn-read">Read Article →</a>
        </div>
      </div>
    </div>
  );
};

export default ArticleCard;
