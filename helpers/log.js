export const remoteLog = async (logMsg) => {
  const response = await fetch(`https://gfm-papi.firebaseio.com/logs.json`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({      
      msg: logMsg,
      // date: logDate
    }),
  });
  if (!response.ok) {
    console.log('could not log remotely!');
  }
};
