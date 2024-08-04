import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Navigation from './components/Navigation';

function App() {
  return (
    <Router>
      <div>
        <Navigation />
        <Switch>
          <Route path="/login" component={Login} />
          <Route path="/register" component={Register} />
          {/* 추가 라우트 */}
        </Switch>
      </div>
    </Router>
  );
}

export default App;