import React from 'react';
import { Layers, Search, Bell, User } from 'lucide-react';

const Navbar = () => {
  return (
    <nav className="navbar glass">
      <div className="container">
        <div className="nav-logo gradient-text">
          TechSphere
        </div>
        
        <div className="nav-links">
          <a href="#" className="nav-link">Home</a>
          <a href="#" className="nav-link">AI & ML</a>
          <a href="#" className="nav-link">Cybersecurity</a>
          <a href="#" className="nav-link">Cloud</a>
        </div>
        
        <div style={{ display: 'flex', gap: '16px', color: 'var(--text-secondary)' }}>
          <Search size={20} style={{ cursor: 'pointer' }} />
          <Bell size={20} style={{ cursor: 'pointer' }} />
          <User size={20} style={{ cursor: 'pointer' }} />
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
