package com.google.codelab.mlkit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Translation extends Activity {

    static public class Morpheme {
        final String text;
        final String type;
        Integer count;
        int id; // 문장 요소 순서 저장 -> JSON의 순서 보장 X을 방지하기 위해

        public Morpheme(String text, String type, Integer count, int id) {
            this.text = text;
            this.type = type;
            this.count = count;
            this.id = id;
        }

        public int getId () {
            return this.id;
        }
    }

    static public class NameEntity {
        final String text;
        final String type;
        Integer count;
        int id;

        public NameEntity(String text, String type, Integer count, int id) {
            this.text = text;
            this.type = type;
            this.count = count;
            this.id = id;
        }
    }

    public class Morphemecomparator implements Comparator<Morpheme> {

        public int compare(Morpheme m1, Morpheme m2) {
            int id1 = m1.getId();
            int id2 = m2.getId();

            if (id1 < id2) {
                return -1;
            } else if (id1 > id2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    // 최종 결과값 확인
    static String output = "";
    static String original = "";
    static List<Morpheme> morphemes = null;
    static String pre_text = "";
    static ArrayList<String> textArray;
    static ArrayList<String> textArray2;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trn);

        // 언어 분석 기술(문어)
        String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
        String accessKey = "5fe3e0e6-8c3d-458f-b415-2ef6d1a1cc65";    // 발급받은 API Key
        String analysisCode = "wsd";   // 언어 분석 코드
        String text = "";           // 분석할 텍스트 데이터
        Gson gson = new Gson();

        textArray  = new ArrayList<>();
        textArray2  = new ArrayList<>();

        // 이미지 텍스트 받기
        Intent intent = getIntent();
        // 언어 분석 기술(문어)
        pre_text = intent.getStringExtra("out_text");
        original = pre_text;
        text = pre_text;

        /*
        "실습실 주의사항 " +
                "1. USB 바이러스 감염 주의 " +
                "(USB 사용 시 포맷하시고 사용하시기 바랍니다.) " +
                "2. 컴퓨터 종료 후 퇴실하시기 바랍니다. " +
                "3. 강의실 내 비품 및 소모품 절도 행위를 금지합니다. " +
                "(CCTV 녹화 중)";
         */

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        argument.put("analysis_code", analysisCode);
        argument.put("text", text);

        request.put("access_key", accessKey);
        request.put("argument", argument);

        URL url;

        try {
            url = new URL(openApiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            Thread thread = new Thread() {
                public void run() {
                    Integer responseCode = null;
                    String responBodyJson = null;
                    LinkedHashMap<String, Object> responeBody = null;

                    DataOutputStream wr = null;

                    try {
                        wr = new DataOutputStream(con.getOutputStream());
                        wr.write(gson.toJson(request).getBytes("UTF-8"));
                        wr.flush();
                        wr.close();

                        responseCode = con.getResponseCode();
                        InputStream is = con.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sb = new StringBuffer();

                        String inputLine = "";

                        while ((inputLine = br.readLine()) != null) {
                            sb.append(inputLine);
                        }

                        responBodyJson = sb.toString();

                        // http 요청 오류 시 처리
                        if (responseCode != 200) {
                            // 오류 내용 출력
                            System.out.println("[error] " + responBodyJson);
                            return;
                        }

                        responeBody = gson.fromJson(responBodyJson, LinkedHashMap.class);
                        Integer result = ((Double) responeBody.get("result")).intValue();
                        Map<String, Object> returnObject;
                        List<LinkedHashMap> sentences;

                        // 분석 요청 오류 시 처리
                        if (result != 0) {
                            // 오류 내용 출력
                            System.out.println("[error] " + responeBody.get("result"));
                            return;
                        }

                        // JSON 작동 확인
                        // output += responBodyJson + "\n";

                        // 분석 결과 활용
                        returnObject = (Map<String, Object>) responeBody.get("return_object");
                        sentences = (List<LinkedHashMap>) returnObject.get("sentence");

                        Map<String, Morpheme> morphemesMap = new LinkedHashMap<String, Morpheme>();
                        Map<String, NameEntity> nameEntitiesMap = new LinkedHashMap<String, NameEntity>();
                        List<NameEntity> nameEntities = null;

                        for (Map<String, Object> sentence : sentences) {
                            // 형태소 분석기 결과 수집 및 정렬
                            List<LinkedHashMap<String, Object>> morphologicalAnalysisResult = (List<LinkedHashMap<String, Object>>) sentence.get("WSD");

                                for (Map<String, Object> morphemeInfo : morphologicalAnalysisResult) {
                                    Double d_id = (Double) morphemeInfo.get("id");
                                    int id = d_id.intValue();

                                    String lemma = (String) morphemeInfo.get("text");
                                    Morpheme morpheme = morphemesMap.get(lemma);

                                    if (morpheme == null) {
                                        morpheme = new Morpheme(lemma, (String) morphemeInfo.get("type"), 1, id);
                                        morphemesMap.put(lemma, morpheme);
                                    } else {
                                        morpheme.count = morpheme.count + 1;
                                    }

                                    // 번역 알고리즘 적용
                                    translate((String) morphemeInfo.get("type"),(String) morphemeInfo.get("text") + " ");
                                    textArray.add(output);
                                    output += " ";
                                }



                                    // 개체명 분석 결과 수집 및 정렬
                            List<Map<String, Object>> nameEntityRecognitionResult = (List<Map<String, Object>>) sentence.get("NE");

                            for (Map<String, Object> nameEntityInfo : nameEntityRecognitionResult) {
                                String name = (String) nameEntityInfo.get("text");
                                Double d_id = (Double) nameEntityInfo.get("id");
                                int id = d_id.intValue();
                                NameEntity nameEntity = nameEntitiesMap.get(name);

                                if (nameEntity == null) {
                                    nameEntity = new NameEntity(name, (String) nameEntityInfo.get("type"), 1, id);
                                    nameEntitiesMap.put(name, nameEntity);
                                } else {
                                    nameEntity.count = nameEntity.count + 1;
                                }
                            }
                        }

                        if (0 < morphemesMap.size()) {
                            morphemes = new ArrayList<Morpheme>(morphemesMap.values());
                            morphemes.sort((morpheme1, morpheme2) -> {
                                return morpheme2.count - morpheme1.count;
                            });
                        }

                        if (0 < nameEntitiesMap.size()) {
                            nameEntities = new ArrayList<NameEntity>(nameEntitiesMap.values());
                            nameEntities.sort((nameEntity1, nameEntity2) -> {
                                return nameEntity2.count - nameEntity1.count;
                            });
                        }

                        // Collections.sort(morphemes, new Morphemecomparator());

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            // 스레드 작업 완료될 때까지 기다리다 종료
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 작업 테스트
        Intent intent2 = new Intent(getApplicationContext(), Video.class);
        intent2.putStringArrayListExtra("textArray", textArray); // 형태소 분석 결과 띄어쓰기 단어로 저장함
        // intent2.putExtra("out_put", "결과값 : " + "\n" + output);
        intent2.putExtra("original", original); // 수어 번역 적용 안 된 원래 문장
        startActivity(intent2);

        output = "";
        finish();   //현재 액티비티 종료
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void translate (String type, String text) {
        text.replace("\n", " "); // 줄바꿈 -> 띄어쓰기로 변환


        // 1. 수화 표현을 위한 문장 요소 제거
        // 동사 파생 접미사(XSV), 선어말 어미(EP), 명사형 전성 어미, 종결 어미 등 제거

        if (text.equals("?")) { // 문장 요소에서 ?를 예외 대상으로 분류
            output += text + " ";

        } else if (text.equals("배이러스 ")) {
            output += "바이러스 ";

        } else if (text.equals("1 ")) {
            output += "첫째";

        } else if (text.equals("2 ")) {
            output += "둘째";

        } else if (text.equals("3 ")) {
            output += "셋째";

        } else if (text.equals("4 ")) {
            output += "넷째";

        } else if (text.equals("주 ")) {
            return;

        } else if (text.equals("의 ")) {
            output += "주의 ";
            return;

        }  else if (!(type.equals("XSV") || type.equals("EP") || type.equals("ETN") || type.equals("EF") || type.equals("SF")
                || type.equals("SS") || type.equals("EP") || type.equals("JKO"))) {
            output += text;
        }

        /*
        output = "실습실 주의 사항\n" +
                "1 USB 바이러스 감염 주의\n" +
                "(USB 사용 시 포맷 고 사용하 바라)\n" +
                "2 컴퓨터 종료 후 퇴실하 바라\n" +
                "3 강의실 내 비품 및 소모품 절도 행위 금지하\n" +
                "(CCTV 녹화 중)";


        /*
        output = "연구실 주의 사항\n" +
                "첫째 /USB/ 바이러스 감염 주의\n" +
                "/USB/ 사용 시 포맷 그리고 사용 바라다\n" +
                "둘째 컴퓨터 종료 후 퇴실 바라다\n" +
                "셋째 강의실 내 물건 절도 행위 금지\n" +
                "(/CCTV/ 녹화 중)";
         */

        // 3. 수어 높임말 용어 변경 및 위치 이동
        // 높임말은 표현이 한정적이므로 직접 변환? (계시다 -> 있다, 잡수시다 -> 먹다)
        // '시'와 같은 높임을 나타내는 선어말 어미는 EP 태그로 따로 분류되어 걸러질 것으로 예상

    }

          /*
           JSONObject jsonObject = new JSONObject(json);
           JSONArray Array = jsonObject.getJSONArray("sentence").getJSONArray("WSD");

        for(int i=0; i<Array.length(); i++) {
                JSONObject object = Array.getJSONObject(i);
                // 1. 수어 표현을 위한 문장 요소 제거
                // 조사(J~) / 어미(E~) / 문법적 요소(S~)
                if (!(object.getString("type").startsWith("J") && object.getString("type").startsWith("E")
                && object.getString("type").startsWith("S"))) {
                // 문법적 요소(SF)에서는 물음표를 얻어내야 함
                }

                // 2. 수어 표현의 변환 및 시제 표현
                    // 의존 명사는 다 NNB로 취급이 되어서 단위성 의존 명사(마리, 명, 그루, 개 등)을 찾아내서 어떻게 제거하는지?
                    // 숫자(SN), 수사(NR) 태그 뒤에 NNB가 오는 경우 제거
                    // 시제를 나타내는 어미가 선어말 어미(EP)에도 포함되어 있는데 어떻게 구분하는지?
                    // EP를 따로 검사하여서 었, 였과 같은 시제 표현을 발견하면 '끝'이라는 수어적 표현으로 변환되게

               // 3. 수어 높임말 용어 변경 및 위치 이동
                    // 높임말은 표현이 한정적이므로 직접 변환? (계시다 -> 있다, 잡수시다 -> 먹다)
                    // '시'와 같은 높임을 나타내는 선어말 어미는 EP 태그로 따로 분류되어 걸러질 것으로 예상
            }

           */

};
