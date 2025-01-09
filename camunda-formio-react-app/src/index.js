import React from "react";
import { render } from "react-dom";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Root from "./containers/Root";
import configureStore from "./store/configureStore";
import "./css/index.css";

const store = configureStore();

render(
    <Router>
        <Routes>
            <Route path="/*" element={<Root store={store} />} />
        </Routes>
    </Router>,
    document.getElementById("root")
);
