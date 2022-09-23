package com.google.codelab.mlkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        getMenuInflater().inflate(R.menu.menu_fav,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_fav).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);//검색버튼 클릭시 화면에 꽉차게
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            //검색완료시 이벤트 제어부
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getApplicationContext(),"검색",Toast.LENGTH_SHORT).show();
                return false;
            }
            //검색어 입력시 이벤트 제어부
            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(getApplicationContext(),"입력중",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.search_fav){
            //To Do : 검색했을 때 쿼리 구현
            Toast.makeText(getApplicationContext(),"검색완료",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
