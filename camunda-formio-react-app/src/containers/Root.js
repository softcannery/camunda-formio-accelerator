import React from "react";
import PropTypes from "prop-types";
import { Provider } from "react-redux";
import { Route } from "react-router-dom";
import App from "./App";
import StartProcessPage from "./StartProcessPage";
import StartProcessListPage from "./StartProcessListPage";
import TasklistPage from "./TasklistPage";
import Header from "../components/Header";
import Footer from "../components/Footer";

const Root = ({ store }) => (
  <Provider store={store}>
    <div>
      <Header />
      <Route path="/" element={<App />} end />
      <Route path="/startprocess/key/:processDefinitionId" element={<StartProcessPage />} />
      <Route path="/startprocess/list" element={<StartProcessListPage />} />
      <Route path="/tasklist" element={<TasklistPage />} end />
      <Route path="/tasklist/:processDefinitionId/:taskId" element={<TasklistPage />} />
    </div>
  </Provider>
);

Root.propTypes = {
  store: PropTypes.object.isRequired,
};
export default Root;
