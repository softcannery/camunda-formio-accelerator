import React, { useEffect } from "react";
import { connect } from "react-redux";
import { Link, useParams } from "react-router-dom";
import { List, Grid, Container } from "semantic-ui-react";
import { loadTasks } from "../actions";
import Taskform from "../components/Taskform";
import sortBy from "lodash/sortBy";

const TasklistPage = ({ loadTasks, task, processDefinitionId }) => {
  const { processDefinitionIdParam } = useParams();

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const renderItem = (task) => (
      <List.Item key={task.id}>
        <List.Icon name="browser" size="large" verticalAlign="middle" />
        <List.Content>
          <Link to={`/tasklist/${task.processDefinitionId}/${task.id}`}>
            <List.Header>{task.name}</List.Header>
            <List.Description>{task.created}</List.Description>
          </Link>
        </List.Content>
      </List.Item>
  );

  let taskForm = processDefinitionId ? (
      <Taskform />
  ) : (
      <Container>Please choose task.</Container>
  );

  if (!task) {
    return <Container>Loading tasks</Container>;
  } else {
    task = sortBy(task, "created").reverse();
    return (
        <Grid divided>
          <Grid.Row>
            <Grid.Column width={4}>
              <List divided relaxed>
                {task.map((item) => renderItem(item))}
              </List>
            </Grid.Column>
            <Grid.Column width={12}>{taskForm}</Grid.Column>
          </Grid.Row>
        </Grid>
    );
  }
};

const mapStateToProps = (state) => ({
  ...state.entities,
});

export default connect(mapStateToProps, { loadTasks })(TasklistPage);
