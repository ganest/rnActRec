import {AppRegistry} from 'react-native';
import React from 'react';
import App from './App';
import {name as appName} from './app.json';

import SQLite from 'react-native-sqlite-storage';

import { insertEvent} from './helpers/db';

import {remoteLog} from './helpers/log';

const G4MActivityRecognition = async (taskData) => {
  const now = `${new Date(Date.now()).toLocaleString('el-GR')}`;
  console.log(`Receiving New AR Alert! ${now}`);
  console.log(`taskData! ${JSON.stringify(taskData)}`);

  if (taskData && taskData.label)
    // TODO: I have to check the undefined issue...    
    try { 
      const db = SQLite.openDatabase('logs.db');
      const dbResult = await insertEvent(db,
        `${taskData.label}, ${taskData.confidence}, ${now}`,
      );
    } catch (err) {
      console.log(err);
    }
    
};

AppRegistry.registerHeadlessTask(
  'G4MActivityRecognition',
  () => G4MActivityRecognition,
);
AppRegistry.registerComponent(appName, () => App);
