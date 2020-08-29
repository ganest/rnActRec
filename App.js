import React, {useEffect, useState} from 'react';
import {StyleSheet, Text, View, FlatList, TouchableOpacity} from 'react-native';
import {connect} from 'react-redux';
import G4MActivityRecognition from './G4MActivityRecognition';

import {
  init,
  fetchLoggedEvents,
  clearLoggedEvents,
} from './helpers/db';

import {remoteLog} from './helpers/log';

init()
  .then(() => {
    console.log('Initialized database');
  })
  .catch((err) => {
    console.log('Initializing db failed.');
    console.log(err);
  });

function Item({title}) {
  return (
    <View style={styles.item}>
      <Text style={styles.title}>{title}</Text>
    </View>
  );
}

export default function App() {
  const [isTracking, setIsTracking] = useState(false);
  const [showLog, setShowLog] = useState(false);
  const [logData, setLogData] = useState([]);

  const startTracking = async () => {
    if (!isTracking) {
      await G4MActivityRecognition.startARTracking();
      setShowLog(false);
      setIsTracking(true);
      console.log('Tracking started');
    } else {
      await G4MActivityRecognition.stopARTracking();
      setIsTracking(false);
      console.log('Tracking stopped');
    }
  };

  const resultsArray = (dbResult) => {
    let results = [];
    const resSize = dbResult.rows.length;
    for (let i = 0; i < resSize; ++i) results.push(dbResult.rows.item(i));

    return results;
  };

  const loadLogs = async () => {
    try {
      if (!showLog) {
        setShowLog(true);
        const dbResult = await fetchLoggedEvents();
        setLogData(resultsArray(dbResult));
      } else {
        setShowLog(false);
      }
    } catch (err) {
      console.log(err);
    }
  };

  const clearLogs = async () => {
    try {
      const dbResult = await clearLoggedEvents();
      setLogData([]);
    } catch (err) {
      console.log(err);
    }
  };

  useEffect(() => {
    const config = async () => {};

    config();
  }, []);

  let logArea;
  if (showLog) {
    if (logData.length > 0) {
      logArea = (
        <FlatList
          data={logData}
          renderItem={({item}) => <Item title={item.event_data} />}
          keyExtractor={(item) => `${item.id}`}
        />
      );
    } else {
      logArea = <Text>Empty Log</Text>;
    }
  }
  return (
    <View style={styles.container}>
      <View style={styles.buttonsContainer}>
        <TouchableOpacity style={styles.buttonStyle} onPress={startTracking}>
          <Text style={styles.buttonText}>
            {isTracking ? 'Stop tracking1' : 'Start tracking'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.buttonStyle} onPress={loadLogs}>
          <Text style={styles.buttonText}>
            {showLog ? 'Hide Logs' : 'Show Logs'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.buttonStyle} onPress={clearLogs}>
          <Text style={styles.buttonText}>Clear Logs</Text>
        </TouchableOpacity>
      </View>
      <View style={styles.logContainer}>{logArea}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    paddingTop: 24,
  },
  buttonsContainer: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  logContainer: {
    flex: 10,
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  buttonStyle: {
    display: 'flex',
    height: 25,
    borderRadius: 5,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 8,

    backgroundColor: '#2AC062',
    shadowColor: '#2AC062',
    shadowOpacity: 0.4,
    shadowOffset: {height: 10, width: 0},
    shadowRadius: 20,
  },

  buttonText: {
    fontSize: 12,
    textTransform: 'uppercase',
    color: '#FFFFFF',
  },

  listArea: {
    backgroundColor: '#f0f0f0',
    flex: 1,
    paddingTop: 32,
  },
  item: {
    backgroundColor: 'powderblue',
    padding: 10,
    marginVertical: 2,
    marginHorizontal: 8,
  },
  title: {
    fontSize: 10,
  },
});
