import CustomerList from './components/CustomerList';

function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <h1 className="welcome-title">Berry Books 管理者画面</h1>
        <hr />
      </header>
      <main>
        <CustomerList />
      </main>
    </div>
  );
}

export default App;

