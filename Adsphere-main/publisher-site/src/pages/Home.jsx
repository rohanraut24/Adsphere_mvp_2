import React from 'react';
import Navbar from '../components/Navbar';
import AdPlacement from '../components/AdPlacement';
import ArticleCard from '../components/ArticleCard';

const MOCK_ARTICLES = [
  {
    id: 1,
    title: 'The Future of AI in Enterprise Security',
    excerpt: 'How machine learning models are predicting and preventing zero-day attacks before they happen.',
    imageUrl: 'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=600&q=80',
    author: 'Sarah Chen',
    readTime: 5
  },
  {
    id: 2,
    title: 'Quantum Computing: A Developer\'s Guide',
    excerpt: 'Getting started with quantum algorithms and understanding how they differ from classical computing.',
    imageUrl: 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=600&q=80',
    author: 'Michael Rodriguez',
    readTime: 8
  },
  {
    id: 3,
    title: 'Optimizing React Applications for Scale',
    excerpt: 'Advanced techniques for managing state, memoization, and bundle splitting in massive codebases.',
    imageUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=600&q=80',
    author: 'Emma Watson',
    readTime: 6
  },
  {
    id: 4,
    title: 'The Rise of Edge Computing Architectures',
    excerpt: 'Moving logic closer to the user to reduce latency and bandwidth usage in IoT applications.',
    imageUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80',
    author: 'David Kim',
    readTime: 4
  }
];

const Home = () => {
  // Use generic dummy placement IDs (1, 2, 3) 
  // Change these to valid placement IDs from the AdSphere database for real tracking
  const BANNER_PLACEMENT_ID = 1;
  const SIDEBAR_PLACEMENT_ID = 2;

  return (
    <>
      <Navbar />
      
      <main className="container">
        {/* Top Banner Ad Space */}
        <section style={{ margin: '48px 0' }} className="animate-fade-in">
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '1px' }}>
            Advertisement
          </p>
          <AdPlacement 
            placementId={BANNER_PLACEMENT_ID} 
            type="banner" 
            fallbackImageUrl="https://images.unsplash.com/photo-1611162617474-5b21e879e113?auto=format&fit=crop&w=1200&h=90&q=80" 
          />
        </section>

        <div className="main-layout">
          {/* Left Column: Content */}
          <div className="content-column">
            <h1 style={{ fontSize: '36px', marginBottom: '32px', letterSpacing: '-1px' }}>Latest Tech Insights</h1>
            
            <div className="article-grid">
              {MOCK_ARTICLES.map(article => (
                <ArticleCard key={article.id} {...article} />
              ))}
            </div>
            
            <div style={{ marginTop: '48px', textAlign: 'center' }}>
              <button className="glass" style={{ padding: '12px 24px', borderRadius: '8px', color: 'white', border: '1px solid var(--border-color)', cursor: 'pointer', fontWeight: '500' }}>
                Load More Articles
              </button>
            </div>
          </div>

          {/* Right Column: Sidebar */}
          <aside className="sidebar-column">
            <div className="glass-card sticky" style={{ position: 'sticky', top: '100px' }}>
              <h3 style={{ marginBottom: '24px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
                Sponsored
              </h3>
              
              <AdPlacement 
                placementId={SIDEBAR_PLACEMENT_ID} 
                type="sidebar" 
                fallbackImageUrl="https://images.unsplash.com/photo-1542744094-24638ea095b5?auto=format&fit=crop&w=300&h=600&q=80" 
              />
              
              <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '16px', textAlign: 'center' }}>
                Looking to reach millions of developers? <a href="#" style={{ color: 'var(--accent-primary)' }}>Advertise with us.</a>
              </p>
            </div>
          </aside>
        </div>
      </main>
      
      <footer className="glass" style={{ padding: '48px 0', marginTop: '96px', borderTop: '1px solid var(--border-color)' }}>
        <div className="container" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>
          <p>© 2026 TechSphere Media. All rights reserved.</p>
        </div>
      </footer>
    </>
  );
};

export default Home;
