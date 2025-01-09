import React from "react";
import Viewer from "bpmn-js/lib/NavigatedViewer";

export default class BPMNDiagram extends React.Component {
  constructor(props) {
    super(props);
    this.viewer = new Viewer();
    this.state = {
      loaded: false,
    };
  }

  storeContainer = (container) => {
    this.container = container;
  };

  componentDidUpdate(prevProps) {
    if (prevProps.xml !== this.props.xml) {
      this.setState({ loaded: false });
      this.importXML(this.props.xml);
    }
  }

  importXML(xml) {
    if (!xml) {
      console.error("No XML to display");
      return;
    }

    this.viewer.importXML(xml, (err) => {
      if (err) {
        console.error("Error importing XML:", err);
      } else {
        const canvas = this.viewer.get("canvas");
        canvas.resized();
        canvas.zoom("fit-viewport", "auto");
        this.setState({ loaded: true });
      }
    });
  }

  componentDidMount() {
    this.viewer.attachTo(this.container);
    if (this.props.xml) {
      this.importXML(this.props.xml);
    }
  }

  render() {
    return (
        <div className="BPMNDiagram" style={this.props.style} ref={this.storeContainer}>
          {this.state.loaded &&
              this.props.children &&
              React.cloneElement(this.props.children, { viewer: this.viewer })}
        </div>
    );
  }
}
