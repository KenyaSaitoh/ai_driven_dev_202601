import React, { ReactNode } from 'react';
import Header from './Header';
import '../styles/Layout.css';

interface LayoutProps {
  children: ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <div className="layout">
      <Header />
      <main className="main-content">
        {children}
      </main>
      <footer className="footer">
        <p>&copy; 2024 Back Office System. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default Layout;

